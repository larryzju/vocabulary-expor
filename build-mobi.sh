#!/bin/sh

words=$1
bname=`date +'%Y-%m-%d-%H-vocabulary'`

./batch-word-query ${words} | tee ${bname}.txt
./aha -f ${bname}.txt > ${bname}.html
kindlegen ${bname}.html
