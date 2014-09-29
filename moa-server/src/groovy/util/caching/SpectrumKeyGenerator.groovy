package util.caching

import org.apache.log4j.Logger
import org.springframework.cache.interceptor.KeyGenerator

import java.lang.reflect.Method

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/29/14
 * Time: 12:25 PM
 */
class SpectrumKeyGenerator implements KeyGenerator {

    Logger logger = Logger.getLogger(getClass())

    @Override
    Object generate(Object target, Method method, Object... params) {
        StringBuilder key = new StringBuilder()

        key.append(target.getClass().getName())
        key.append("-")
        key.append(method.toString())
        key.append("-")
        params.each {
            key.append(it.toString())
        }


        logger.info("generated key: ${key.toString()}")
        return key.toString()
    }
}
