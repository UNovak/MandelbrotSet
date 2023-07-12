#!/bin/zsh

pwd

ls -al

javac -cp /Users/urbannovak/mpj/lib/mpj.jar Calculate.java

/Users/urbannovak/mpj/bin/mpjrun.sh -np $1 Calculate $2 $3