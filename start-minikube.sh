#!/bin/bash -ev
(minikube delete || true) &>/dev/null
minikube start --memory 8192
#minikube start --memory 8192 --cpus 10 --extra-config=apiserver.authorization-mode=RBAC
#Setup Docker engine context to point to Minikube
eval $(minikube docker-env)
#Enable Ingress Controller
minikube addons enable ingress
kubectl --namespace=kube-system create clusterrolebinding add-on-cluster-admin --clusterrole=cluster-admin --serviceaccount=kube-system:default

sbt docker:publishLocal
sbt "deploy minikube"
