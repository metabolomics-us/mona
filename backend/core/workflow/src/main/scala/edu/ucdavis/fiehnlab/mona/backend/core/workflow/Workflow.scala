package edu.ucdavis.fiehnlab.mona.backend.core.workflow

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils


/**
  * defines a standard processing workflow
  */
@Component
class Workflow extends BeanPostProcessor with LazyLogging{

  /**
    * executes the workflow for the given spectrum
    *
    * @param spectrum
    * @return
    */
  def run(spectrum:Spectrum) = ???
/**
  *override def postProcessAfterInitialization(bean: scala.Any, name: String): Any = {
    *scanBeanForAnnotations(bean,name)
    *bean
  *}

  *override def postProcessBeforeInitialization(bean: scala.Any, name: String): Any = bean
*/

  /**
    * attempts to find our required annotations
    * and builds the internal graph based on this
    *
    * @param bean
    */
  def scanBeanForAnnotations(bean:Any) : Unit = {

    val step:Step = bean.getClass.getAnnotation(classOf[Step])

    if(step != null){
    }

  }

  override def postProcessAfterInitialization(o: scala.Any, s: String): AnyRef = ???

  override def postProcessBeforeInitialization(o: scala.Any, s: String): AnyRef = ???
}
