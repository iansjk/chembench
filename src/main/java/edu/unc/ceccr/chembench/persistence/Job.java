package edu.unc.ceccr.chembench.persistence;

import edu.unc.ceccr.chembench.taskObjects.WorkflowTask;

import javax.persistence.*;
import java.util.Date;

@Entity()
@Table(name = "cbench_job")
public class Job {

    @Transient
    public WorkflowTask workflowTask; //contains one modelingTask, predictionTask, or datasetTask.
    private Long id;
    private String lsfJobId; //the job ID assigned by LSF. Used to track the job.
    private String userName;
    private String jobName;
    private String status; //basic status. {queued, pending, started, finished, permissionRequired, deleted}
    private String message; //a more detailed status message
    private String jobList; //which jobList it's in. {NONE, INCOMING, LSF, LOCAL, ERROR}
    private String jobType; //can be one of {MODELING, PREDICTION, DATASET};
    private Long lookupId; //a primary key for the datasetJob, modelingJob, or predictionJob tables (as determined by
    // jobType)
    private int numCompounds;
    private int numModels;
    private Date timeCreated;
    private Date timeStarted;
    private Date timeStartedByLsf; //jobs may remain pending in LSF for a long time before actually started.
    private Date timeFinished;
    private String emailOnCompletion; //"true" or "false"
    private String timeFinishedEstimate;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "lsfJobId")
    public String getLsfJobId() {
        return lsfJobId;
    }

    public void setLsfJobId(String lsfJobId) {
        this.lsfJobId = lsfJobId;
    }

    @Column(name = "userName")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "jobName")
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Column(name = "jobType")
    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    @Column(name = "jobList")
    public String getJobList() {
        return jobList;
    }

    public void setJobList(String jobList) {
        this.jobList = jobList;
    }

    @Column(name = "lookupId")
    public Long getLookupId() {
        return lookupId;
    }

    public void setLookupId(Long lookupId) {
        this.lookupId = lookupId;
    }

    @Column(name = "numCompounds")
    public int getNumCompounds() {
        return numCompounds;
    }

    public void setNumCompounds(int numCompounds) {
        this.numCompounds = numCompounds;
    }

    @Column(name = "numModels")
    public int getNumModels() {
        return numModels;
    }

    public void setNumModels(int numModels) {
        this.numModels = numModels;
    }

    @Column(name = "timeCreated")
    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    @Column(name = "timeStarted")
    public Date getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(Date timeStarted) {
        this.timeStarted = timeStarted;
    }

    @Column(name = "timeStartedByLsf")
    public Date getTimeStartedByLsf() {
        return timeStartedByLsf;
    }

    public void setTimeStartedByLsf(Date timeStartedByLsf) {
        this.timeStartedByLsf = timeStartedByLsf;
    }

    @Column(name = "timeFinished")
    public Date getTimeFinished() {
        return timeFinished;
    }

    public void setTimeFinished(Date timeFinished) {
        this.timeFinished = timeFinished;
    }

    @Column(name = "emailOnCompletion")
    public String getEmailOnCompletion() {
        return emailOnCompletion;
    }

    public void setEmailOnCompletion(String emailOnCompletion) {
        this.emailOnCompletion = emailOnCompletion;
    }

    @Transient
    public String getTimeFinishedEstimate() {
        return timeFinishedEstimate;
    }

    public void setTimeFinishedEstimate(String timeFinishedEstimate) {
        this.timeFinishedEstimate = timeFinishedEstimate;
    }

}
