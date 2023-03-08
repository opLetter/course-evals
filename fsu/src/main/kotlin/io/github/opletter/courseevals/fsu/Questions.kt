package io.github.opletter.courseevals.fsu

val QuestionsLimited = listOf(
    "The course materials helped me understand the subject matter.",
    "The work required of me was appropriate based on course objectives.",
    "The tests, project, etc. accurately measured what I learned in this course.",
    "This course encouraged me to think critically.",
    "I learned a great deal in this course.",
    "Instructor(s) provided clear expectations for the course.",
    "Instructor(s) communicated effectively.",
    "Instructor(s) stimulated my interest in the subject matter.",
    "Instructor(s) provided helpful feedback on my work.",
    "Instructor(s) demonstrated respect for students.",
    "Instructor(s) demonstrated mastery of the subject matter.",
    "Overall course content rating.",
    "Overall rating for Instructor(s)"
)
val QuestionsComplete = QuestionsLimited + listOf(
    "What is your year in school?",
    "What is your cumulative GPA?",
    "What grade do you expect to receive in this course?",
    "Is this a required course for you?"
)

// chatGPT suggestions:
// Effective course materials.
// Appropriate course requirements.
// Accurate assessment of learning.
// Encouraged critical thinking.
// Significant learning achieved.
// Clear course expectations.
// Effective instructor communication.
// Stimulating subject matter.
// Helpful feedback provided.
// Instructor demonstrated respect.
// Instructor mastery demonstrated.
// Positive course content rating.
// Positive Instructor rating.
val QuestionsShortened = listOf(
    "Helpful course materials",
    "Appropriate amount of work",
    "Accurate assessment of learning",
    "Encouraged critical thinking",
    "Learned a great deal",
    "Instructor provided clear expectations",
    "Instructor communicated effectively",
    "Instructor stimulated interest",
    "Instructor provided helpful feedback",
    "Instructor demonstrated respect",
    "Instructor demonstrated mastery of subject matter",
    "Overall course content rating",
    "Overall rating for Instructor",
)