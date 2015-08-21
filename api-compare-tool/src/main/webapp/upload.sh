#!/bin/bash
#Usage: upload.sh <file id> <file path>
taskId=$1
file=$2
[ -z "$taskId" ] && echo "Please specify a task id." && exit 0
[ -z "$file" ] && echo "Please specify a file path." && exit 0
[ ! -f "$file" ] && echo "Specified file is not exist." && exit 0
creator=$(curl -sb -H "Accept: application/json" "http://apicomparetooltest.server.hulu.com:8080/api/tasks/get_creator?id=$taskId")
[ -z "$creator" ] && echo "Task is not exist with specified id." && exit 0
[ "$creator" != "$USER" ]  && echo "Not creator of the task." && exit 0
curl -i -X PUT -T "$file" -L "http://10.16.60.114:50070/webhdfs/v1/user/search/$taskId.txt?op=CREATE&user.name=search&overwrite=true&blocksize=1048576"

