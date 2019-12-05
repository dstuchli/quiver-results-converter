#!/bin/bash

if [[ -z $@ ]]
then
    echo "Argument is required"
    exit 1
fi

if [[ $@ =~ .*(sender|receiver)-transfers\.csv\.xz ]]
then
    xz -d "$@"
    echo "Decompression was successful"
else 
    echo "Unknown file to unzip"
fi