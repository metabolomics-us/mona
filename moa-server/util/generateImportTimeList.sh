cat $1 |  grep server.SpectraUploadJob |grep 'which took' | awk '{print $1 " " $2 " " $3 " " $16 " " $19 " " $22 " " $26}' | sed  's/s//g'
