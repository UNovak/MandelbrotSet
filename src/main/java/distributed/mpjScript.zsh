#!/bin/zsh

pwd

ls -al

javac -cp /Users/urbannovak/.mpj/lib/mpj.jar Calculate.java

# $1 = number of cores to use
# $2 = width
# $3 = height
# $4 = maxIterations
/Users/urbannovak/.mpj/bin/mpjrun.sh -np $1 Calculate $2 $3 $4

