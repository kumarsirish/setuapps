while :
do
sleep 300
groovy  /home/opc/setuapps/src/parser.groovy >>/home/opc/setuapps/src/run.log 2>/dev/null
done
