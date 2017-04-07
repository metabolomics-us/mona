package edu.ucdavis.fiehnlab.mona.backend.core.workflow.config

import org.springframework.context.annotation.{ComponentScan, Configuration}

/**
  * Created by wohlgemuth on 3/14/16.
  */
@Configuration
@ComponentScan(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.workflow"))
class WorkflowConfiguration
