package edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.QueryExport
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 5/26/16.
  */
@Repository
@Profile(Array("mona.persistence.downloader"))
trait QueryExportRepository extends JpaRepository[QueryExport, String]
