package pl.databucket.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of running (or previewing) the jobs of a keyword ({@link Databucket#runJobs(String)}): one entry per job,
 * in the order they ran (by id). Some jobs may have failed while others ran - check {@link #getErrors()}.
 */
public final class JobsRun {

    private final String keyword;
    private final boolean preview;
    private final int ok;
    private final int errors;
    private final List<Job> jobs;

    private JobsRun(String keyword, boolean preview, int ok, int errors, List<Job> jobs) {
        this.keyword = keyword;
        this.preview = preview;
        this.ok = ok;
        this.errors = errors;
        this.jobs = jobs;
    }

    static JobsRun of(JsonNode json) {
        List<Job> jobs = new ArrayList<>();
        for (JsonNode job : json.path("jobs"))
            jobs.add(new Job(job.path("id").asLong(), job.path("name").asText(null), job.path("task").asText(null),
                    job.path("bucket").asText(null), job.path("status").asText(null),
                    job.path("count").isNumber() ? job.path("count").asLong() : null, job.path("message").asText(null)));
        return new JobsRun(json.path("keyword").asText(null), json.path("preview").asBoolean(),
                json.path("ok").asInt(), json.path("errors").asInt(), Collections.unmodifiableList(jobs));
    }

    /** The keyword as given. */
    public String getKeyword() {
        return keyword;
    }

    /** True: nothing was changed, the counts are the rows each job would run on. */
    public boolean isPreview() {
        return preview;
    }

    /** The jobs that ran without an error. */
    public int getOk() {
        return ok;
    }

    /** The jobs that failed. */
    public int getErrors() {
        return errors;
    }

    /** Each job, by id. */
    public List<Job> getJobs() {
        return jobs;
    }

    @Override
    public String toString() {
        return "JobsRun{keyword=" + keyword + ", preview=" + preview + ", ok=" + ok + ", errors=" + errors + "}";
    }

    /** One job of the keyword and what its task did. */
    public static final class Job {
        private final long id;
        private final String name;
        private final String task;
        private final String bucket;
        private final String status;
        private final Long count;
        private final String message;

        Job(long id, String name, String task, String bucket, String status, Long count, String message) {
            this.id = id;
            this.name = name;
            this.task = task;
            this.bucket = bucket;
            this.status = status;
            this.count = count;
            this.message = message;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        /** The name of the task. */
        public String getTask() {
            return task;
        }

        /** The name of the bucket. */
        public String getBucket() {
            return bucket;
        }

        /** OK or ERROR. */
        public String getStatus() {
            return status;
        }

        public boolean isOk() {
            return "OK".equals(status);
        }

        /** The number of data rows; null when the job failed before counting. */
        public Long getCount() {
            return count;
        }

        /** What the task did, or why it failed. */
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "Job{" + name + ", " + status + ", count=" + count + "}";
        }
    }
}
