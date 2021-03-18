# Reclaim disk space
time docker rmi node:10 node:12 mcr.microsoft.com/azure-pipelines/node8-typescript:latest
time sudo rm -rf /usr/share/dotnet
time sudo rm -rf /usr/share/swift