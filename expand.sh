#!/bin/bash

for f in $(find . -name "*.java");
do
    expand -t 4 $f > /tmp/expand
    mv /tmp/expand $f
done
