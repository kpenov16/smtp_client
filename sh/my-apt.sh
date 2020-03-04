#!/bin/bash

read -p 'Enter package name: ' package

read -p 'Enter s to install from source or p from dpkg/rpm: ' source

read -p 'Enter the download link: ' downloadLink

echo package: $package, source: $source, downloadLink: $downloadLink

inst_path=/usr/local/src

echo $(ls -ldh $inst_path | cut -d " " -f1) 
gr=$(ls -ldh $inst_path | cut -d " " -f1| cut -c 8-10)

if [[ "$gr" == *"-"* ]]; then
  echo setting others to have +rwx
  sudo chmod o+rwx $inst_path
fi



# https://www.pluralsight.com/blog/it-ops/linux-file-permissions
# https://linuxize.com/post/how-to-check-if-string-contains-substring-in-bash/
# https://stackoverflow.com/questions/428109/extract-substring-in-bash

