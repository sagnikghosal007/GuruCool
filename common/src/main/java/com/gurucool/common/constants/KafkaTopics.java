package com.gurucool.common.constants;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String USER_REGISTERED = "user.registered";
    public static final String USER_EMAIL_VERIFICATION = "user.email.verification";
    public static final String MENTOR_VERIFICATION_REQUESTED = "mentor.verification.requested";
    public static final String MENTOR_VERIFIED = "mentor.verified";
    public static final String SESSION_BOOKED = "session.booked";
    public static final String SESSION_CANCELLED = "session.cancelled";
    public static final String SESSION_COMPLETED = "session.completed";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_REFUNDED = "payment.refunded";
    public static final String PAYMENT_REFUND_REQUESTED = "payment.refund.requested";
    public static final String WAITLIST_PROMOTED = "waitlist.promoted";
    public static final String NOTIFICATION_SEND = "notification.send";
}
