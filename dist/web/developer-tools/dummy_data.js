// select vulndb
db = db.getSiblingDB("vulndb");

// delete existing data
db.message.deleteMany({});

// insert dummy data

db.message.insertMany(
    [
        {
            user_di: 123,
            sent_by_user: true,
            content: "Hello, how are you today?",
            sent_at: new Date("2025-01-15T10:31:00Z"),
            created_at: new Date("2025-01-15T10:31:00Z"),
            updated_at: new Date("2025-01-15T10:31:00Z")
        },
        {
            user_di: 123,
            sent_by_user: false,
            content: "I'm doing great! How can I help you?",
            sent_at: new Date("2025-01-15T10:31:30Z"),
            created_at: new Date("2025-01-15T10:31:30Z"),
            updated_at: new Date("2025-01-15T10:31:30Z")
        },
        {
            user_di: 123,
            sent_by_user: true,
            content: "I need help with MongoDB aggregation pipelines.",
            sent_at: new Date("2025-01-15T10:32:00Z"),
            created_at: new Date("2025-01-15T10:32:00Z"),
            updated_at: new Date("2025-01-15T10:32:00Z")
        },
        {
            user_di: 999,
            sent_by_user: true,
            content: "What's the weather like?",
            sent_at: new Date("2025-01-20T14:15:00Z"),
            created_at: new Date("2025-01-20T14:15:00Z"),
            updated_at: new Date("2025-01-20T14:15:00Z")
        }
    ]
);

// example aggregation: get all messages for user_nr 123 sorted by sent_at
result = db.message.find({user_di: 123}).sort({sent_at: 1});
console.log(result.toArray());