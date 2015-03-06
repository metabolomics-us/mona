package moa.server

import grails.plugins.quartz.JobDescriptor
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher

/**
 * based on the work found here:
 *
 * https://github.com/grails-plugins/grails-quartz/blob/master/grails-app/services/grails/plugins/quartz/JobManagerService.groovy
 */
class QueueService {

    static transactional = false

    Scheduler quartzScheduler

    /**
     * All jobs registered in the Quartz Scheduler, grouped by their corresponding job groups.
     *
     * @return the descriptors
     */
    Map<String, List<JobDescriptor>> getAllJobs() {
        quartzScheduler.jobGroupNames.collectEntries([:]) { group -> [(group):getJobs(group)]}
    }

    /**
     * All jobs registered in the Quartz Scheduler which belong to the specified group.
     *
     * @param group the jobs group name
     * @return the descriptors
     */
    List<JobDescriptor> getJobs(String group) {
        List<JobDescriptor> list = []
        quartzScheduler.getJobKeys(GroupMatcher.groupEquals(group)).each { jobKey ->
            def jobDetail = quartzScheduler.getJobDetail(jobKey)
            if (jobDetail != null) {
                list.add(JobDescriptor.build(jobDetail, quartzScheduler))
            }
        }
        return list
    }

    /**
     * All currently executing jobs.
     *
     * @return the contexts
     */
    List<JobExecutionContext> getRunningJobs() {
        quartzScheduler.getCurrentlyExecutingJobs()
    }

    void pauseJob(String group, String name) {
        quartzScheduler.pauseJob(new JobKey(name, group))
    }

    void resumeJob(String group, String name) {
        quartzScheduler.resumeJob(new JobKey(name, group))
    }

    void pauseTrigger(String group, String name) {
        quartzScheduler.pauseTrigger(new TriggerKey(name, group))
    }

    void resumeTrigger(String group, String name) {
        quartzScheduler.resumeTrigger(new TriggerKey(name, group))
    }

    void pauseTriggerGroup(String group) {
        quartzScheduler.pauseTriggers(GroupMatcher.groupEquals(group))
    }

    void resumeTriggerGroup(String group) {
        quartzScheduler.resumeTriggers(GroupMatcher.groupEquals(group))
    }

    void pauseJobGroup(String group) {
        quartzScheduler.pauseJobs(GroupMatcher.groupEquals(group))
    }

    void resumeJobGroup(String group) {
        quartzScheduler.resumeJobs(GroupMatcher.groupEquals(group))
    }

    void pauseAll() {
        quartzScheduler.pauseAll()
    }

    void resumeAll() {
        quartzScheduler.resumeAll()
    }

    boolean removeJob(String group, String name) {
        quartzScheduler.deleteJob(new JobKey(name, group))
    }

    boolean unscheduleJob(String group, String name) {
        quartzScheduler.unscheduleJobs(quartzScheduler.getTriggersOfJob(new JobKey(name, group))*.key)
    }

    boolean interruptJob(String group, String name) {
        quartzScheduler.interrupt(new JobKey(name, group))
    }
}
