#!/bin/bash

echo "============================="
echo "    DEPLOYING IMAGES         "
echo "============================="

<<<<<<< HEAD
docker push public.ecr.aws/fiehnlab/mona-auth-server:prod
docker push public.ecr.aws/fiehnlab/mona-bootstrap:prod
docker push public.ecr.aws/fiehnlab/mona-config:prod
docker push public.ecr.aws/fiehnlab/mona-curation-runner:prod
docker push public.ecr.aws/fiehnlab/mona-curation-scheduler:prod
docker push public.ecr.aws/fiehnlab/mona-discovery:prod
docker push public.ecr.aws/fiehnlab/mona-download-scheduler:prod
docker push public.ecr.aws/fiehnlab/mona-persistence-server:prod
docker push public.ecr.aws/fiehnlab/mona-proxy:prod
docker push public.ecr.aws/fiehnlab/mona-similarity:prod
docker push public.ecr.aws/fiehnlab/mona-webhooks-server:prod
=======
docker push public.ecr.aws/fiehnlab/mona-auth-server:test
docker push public.ecr.aws/fiehnlab/mona-bootstrap:test
docker push public.ecr.aws/fiehnlab/mona-config:test
docker push public.ecr.aws/fiehnlab/mona-curation-runner:test
docker push public.ecr.aws/fiehnlab/mona-curation-scheduler:test
docker push public.ecr.aws/fiehnlab/mona-discovery:test
docker push public.ecr.aws/fiehnlab/mona-download-scheduler:test
docker push public.ecr.aws/fiehnlab/mona-persistence-server:test
docker push public.ecr.aws/fiehnlab/mona-proxy:test
docker push public.ecr.aws/fiehnlab/mona-similarity:test
docker push public.ecr.aws/fiehnlab/mona-webhooks-server:test
>>>>>>> origin/dev
