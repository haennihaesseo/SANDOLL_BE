# Docker Hub credentials
DOCKER_USERNAME="DOCKER_USERNAME_PLACEHOLDER"
DOCKER_PASSWORD="DOCKER_PASSWORD_PLACEHOLDER"

# Docker login
echo "> 도커 로그인"
echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin

nginx_config_path="/etc/nginx"
all_port=("8080" "8081")
available_port=()
docker_id=$DOCKER_USERNAME
server_name=olllim

docker_ps_output=$(docker ps | grep $server_name)
running_container_name=$(echo "$docker_ps_output" | awk '{print $NF}')
blue_port=$(echo "$running_container_name" | awk -F'-' '{print $NF}')
web_health_check_url=/actuator/health

if [ -z "$blue_port" ]; then
    echo "> 실행 중인 서버의 포트: 없음"
else
    echo "> 실행 중인 서버의 포트: $blue_port"
fi

# 실행 가능한 포트 확인 ( all_port 중 blue_port를 제외한 port )
for item in "${all_port[@]}"; do
    if [ "$item" != "$blue_port" ]; then
        available_port+=("$item")
    fi
done

# 실행 가능한 포트 없으면 끝내기
if [ ${#available_port[@]} -eq 0 ]; then
    echo "> 실행 가능한 포트가 없습니다."
    exit 1
fi

green_port=${available_port[0]}

echo "----------------------------------------------------------------------"
# 1. 공용 Redis 먼저 띄우기 (없으면 띄우고 있으면 유지됨)
echo "> 공용 Redis 상태 확인 및 실행"
sudo -E docker-compose -p redis-common up -d redis

# docker image pull
echo "> 도커 이미지 pull 받기"
sudo docker pull ${docker_id}/${server_name}:latest

# green_port로 서버 실행
echo "> ${green_port} 포트로 서버 실행"

export CONTAINER_NAME="${server_name}-${green_port}"
export TARGET_PORT="${green_port}"
export DOCKER_IMAGE="${docker_id}/${server_name}:latest"

sudo -E docker-compose -p "${CONTAINER_NAME}" up -d olllim
echo "----------------------------------------------------------------------"

# green_port 서버 제대로 실행 중인지 확인
sleep 20
for retry_count in {1..20}
do
    echo "> 서버 상태 체크"
    echo "> curl -s http://localhost:${green_port}${web_health_check_url}"
		# http://localhost:{그린포트}{health check 주소} -> nginx
    response=$(curl -s http://localhost:${green_port}${web_health_check_url})
    up_count=$(echo $response | grep 'UP' | wc -l)

    if [ $up_count -ge 1 ]
    then
        echo "> 서버 실행 성공"
        break
    else
        echo "> 아직 서버 실행 안됨"
        echo "> 응답 결과: ${response}"
    fi
    if [ $retry_count -eq 20 ]
		then
        echo "> 서버 실행 실패"
        docker rm -f ${server_name}-${green_port}

        exit 1
    fi
    sleep 5
done
echo "----------------------------------------------------------------------"

# nginx switching
echo "> nginx 포트 스위칭"
echo "set \$service_url http://127.0.0.1:${green_port};" | sudo tee ${nginx_config_path}/conf.d/service-url.inc
sudo nginx -s reload

sleep 1

echo "----------------------------------------------------------------------"
# nginx를 통해서 서버 접근 가능한지 확인
response=$(curl -s http://localhost${web_health_check_url})
up_count=$(echo $response | grep 'UP' | wc -l)
if [ $up_count -ge 1 ]
then
    echo "> 서버 변경 성공"
else
    echo "> 서버 변경 실패"
    echo "> 서버 응답 결과: ${response}"
    exit 1
fi

# blue_port 서버 있다면 중단
if [ -n "$blue_port" ]; then
    echo "> 기존 ${blue_port}포트 서버 중단"
    sudo -E docker-compose -p "${server_name}-${blue_port}" down
fi