echo "$CIRCLE_BRANCH"

DOCKER_TAG=$1
DOCKER_TAG_COMPONENT=$2
DOCKER_REPO=${DOCKER_TAG_REPO}/${DOCKER_TAG_COMPONENT}

BRANCH=${CIRCLE_BRANCH:-UNSET}
DATE=$(date +"%m%d%y")
BUILD_NUM=${CIRCLE_BUILD_NUM:-UNSET}
TAG=${CIRCLE_TAG:-UNSET}

echo "BRANCH IS $BRANCH"
echo "BUILD_NUM IS $BUILD_NUM"
echo "TAG IS $TAG"

if [ "${BRANCH}" != "UNSET" ]; then
    echo "This is a CI branch build"
    if [ "${BRANCH}" == "master" ]; then
        echo "This is the main branch, not including the branch name in the push"
        DOCKER_RELEASE_TAG=${DATE}-${BUILD_NUM}
    else
        echo "This is not the main branch (${BRANCH}), including the branch name in this push"
        DOCKER_RELEASE_TAG=${BRANCH}-${DATE}-${BUILD_NUM}
    fi
    DOCKER_LATEST_TAG=${BRANCH}-latest
else
    echo "This is a Tag release"
    DOCKER_RELEASE_TAG=$(echo $TAG | cut -d "v" -f 2)
    DOCKER_LATEST_TAG=latest
fi

echo "Version Tag will be: ${DOCKER_RELEASE_TAG}"
echo "Latest Tag will be: ${DOCKER_LATEST_TAG}"

echo "pushing and tagging image"
docker tag ${DOCKER_TAG} ${DOCKER_REPO}:${DOCKER_RELEASE_TAG}
docker tag ${DOCKER_TAG} ${DOCKER_REPO}:${DOCKER_LATEST_TAG}
docker images
docker login -u=${DOCKER_USER} -p=${DOCKER_PASS}
docker push ${DOCKER_REPO}:${DOCKER_RELEASE_TAG}
docker push ${DOCKER_REPO}:${DOCKER_LATEST_TAG}
echo "Done!"