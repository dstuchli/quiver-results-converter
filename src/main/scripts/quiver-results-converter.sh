#!/bin/bash

# Installation paths
localDir="$(dirname $0)"
installDir="$(dirname "${localDir}")"

if [[ ${installDir} == "." ]] ; then
	installDir=".."
fi

MAESTRO_CLASSPATH=""

for jarfile in "${installDir}"/bin/*.jar ; do
    if [[ -z "${MAESTRO_CLASSPATH}" ]] ; then
        MAESTRO_CLASSPATH="${jarfile}"
    else
        MAESTRO_CLASSPATH="${MAESTRO_CLASSPATH}:${jarfile}"
    fi
done

# for jarfile in "${installDir}"/target/*.jar ; do
#     if [[ -z "${MAESTRO_CLASSPATH}" ]] ; then
#         MAESTRO_CLASSPATH="${jarfile}"
#     else
#         MAESTRO_CLASSPATH="${MAESTRO_CLASSPATH}:${jarfile}"
#     fi
# done


for jarfile in "${installDir}"/lib/*.jar ; do
    if [[ -z "${MAESTRO_CLASSPATH}" ]] ; then
        MAESTRO_CLASSPATH="${jarfile}"
    else
        MAESTRO_CLASSPATH="${MAESTRO_CLASSPATH}:${jarfile}"
    fi
done

for jarfile in "${installDir}"/lib/ext/*.jar ; do
    if [[ -z "${MAESTRO_CLASSPATH}" ]] ; then
        MAESTRO_CLASSPATH="${jarfile}"
    else
        MAESTRO_CLASSPATH="${MAESTRO_CLASSPATH}:${jarfile}"
    fi
done

mainClass="org.maestro.cli.main.MainCLI"

java -classpath ${MAESTRO_CLASSPATH} -Dorg.maestro.home="${installDir}" -Djava.awt.headless=true "${mainClass}" "$@"
