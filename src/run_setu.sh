git pull
ps -eaf | grep -i parser
rm -f nohup.out
nohup groovy ./parser_bycalendar.groovy &

