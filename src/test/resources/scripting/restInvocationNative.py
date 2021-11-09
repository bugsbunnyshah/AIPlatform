from java.util import Random
from io.bugsbunny.restClient import OAuthClient
from io.bugsbunny.infrastructure import Http

r = Random()
print(r.nextInt())

clientId = 'PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t'
clientSecret = 'U2jMgxL8zJgYOMmHDYTe6-P9yO6Wq51VmixuZSRCaL-11EPE4WrQOWtGLVnQetdd'
oAuthClient = OAuthClient()
oAuthClient.setHttp(Http())
jwtToken = oAuthClient.getAccessToken(clientId, clientSecret)
print(jwtToken)