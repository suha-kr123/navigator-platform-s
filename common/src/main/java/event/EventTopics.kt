package event

object EventTopics {
    // Advisor Events
    const val ADVISOR_GET_REQUEST = "advisor.get.request"
    const val ADVISOR_GET_RESPONSE = "advisor.get.response"
    const val ADVISOR_VALIDATE_REQUEST = "advisor.validate.request"
    const val ADVISOR_VALIDATE_RESPONSE = "advisor.validate.response"
    const val ADVISOR_GET_BY_MOBILE_REQUEST = "advisor.get.by.mobile.request"
    const val ADVISOR_GET_BY_MOBILE_RESPONSE = "advisor.get.by.mobile.response"

    // Lead Events
    const val LEAD_GET_REQUEST = "lead.get.request"
    const val LEAD_GET_RESPONSE = "lead.get.response"
    const val LEAD_VALIDATE_REQUEST = "lead.validate.request"
    const val LEAD_VALIDATE_RESPONSE = "lead.validate.response"

    // Payment Events
    const val PAYMENT_CREATE_REQUEST = "payment.create.request"
    const val PAYMENT_CREATE_RESPONSE = "payment.create.response"
    const val PAYMENT_GET_REQUEST = "payment.get.request"
    const val PAYMENT_GET_RESPONSE = "payment.get.response"
    const val PAYMENT_UPDATE_REQUEST = "payment.update.request"
    const val PAYMENT_UPDATE_RESPONSE = "payment.update.response"
    const val PAYMENT_DELETE_REQUEST = "payment.delete.request"
    const val PAYMENT_DELETE_RESPONSE = "payment.delete.response"

    // Person Events
    const val PERSON_GET_REQUEST = "person.get.request"
    const val PERSON_GET_RESPONSE = "person.get.response"
    const val PERSON_GET_BY_MOBILE_REQUEST = "person.get.by.mobile.request"
    const val PERSON_GET_BY_MOBILE_RESPONSE = "person.get.by.mobile.response"

    // Task Events
    const val TASK_CREATED = "task.created"
    const val TASK_UPDATED = "task.updated"
    const val TASK_COMPLETED = "task.completed"
    const val TASK_RESCHEDULED = "task.rescheduled"
    const val TASK_DELETED = "task.deleted"
}
