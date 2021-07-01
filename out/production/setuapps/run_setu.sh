ps -eaf | grep -i parser
rm -f nohup.out
echo "SLACK_WEHBHOOK is in bashrc"
echo "See that SLACK_WEBHOOK is set"
echo "SLACK_WEBHOOK=$SLACK_WEBHOOK"
nohup groovy ./parser_bycalendar.groovy &
