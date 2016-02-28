#!/bin/bash

(( EID != 0 )) && exec sudo -- "$0" "$@"

# Install Docker
# Check if kernel meets minimum requirements
MINKER=310
CURFULLKER=`uname -r`
#echo $MINKER
#echo $CURFULLKER
CURKER=`awk -F. '{print$1$2}' <<< $CURFULLKER`
#echo $CURKER
if [ $CURKER -lt $MINKER ]
	then
		echo "Kernel Version Not Met, Exiting..."
		exit 1
fi
echo "Minimum Kernel Version Met, Proceeding..."

# Update package info and CA certificates
printf "\n"
LOG=./docker_install_log
echo `date` &> $LOG
echo `apt-get update` &>> $LOG
printf "\n********************* apt-get Updated *****************************\n"
echo `apt-get install apt-transport-https ca-certificates` &>> $LOG
printf "\n******************** ca-certs Verified ****************************\n"
# Add new GPG Key
echo `apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D` &>> $LOG
printf "\n********************   GPG Key Added   ****************************\n"
printf "\n****************** Replace Docker.list ****************************\n"
echo "deb https://apt.dockerproject.org/repo ubuntu-trusty main" > /etc/apt/sources.list.d/docker.list
#NOTE The above assumes ubuntu-trusty (14.04) installed
echo `apt-get update` &>> $LOG
printf "\n********************* apt-get Updated *****************************\n"
echo `apt-get purge lxc-docker` &>> $LOG
printf "\n******************** lxc-docker purged *****************************\n"

# Install linux-image-extra
echo `apt-get install linux-image-extra-$CURFULLKER` &>> $LOG
printf "\n****************** image-extra installed ***************************\n"

echo `apt-get install apparmor` &>> $LOG
printf "\n******************** apparmor installed ****************************\n"

# Installation of Docker Begins
echo `apt-get install docker-engine` &>> $LOG
printf "\n****************** docker-engine installed ***************************\n"
echo `service docker start`
printf "\n************************ docker started ****************************\n"
echo `docker run hello-world` &>> $LOG
printf "\n****************** docker ran hello-world ***************************\n"

# Create Docker group with root permissions
echo `usermod -aG docker $USER` &>> $LOG
printf "\n************** docker group created, user added ***********************\n"

