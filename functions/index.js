const functions = require("firebase-functions").region("asia-northeast3");
const admin = require("firebase-admin");
admin.initializeApp();

const collectionName = "whats-meal";

const defaultData = {
  hate: ["0"],
  like: ["0"],
};

exports.resetDatabase = functions.pubsub.
    schedule("0 0 * * *").
    timeZone("Asia/Seoul").
    onRun(async (context) => {
      try {
        const db = admin.firestore();
        const documents = await db.collection(collectionName).get();
        const batch = db.batch();

        documents.forEach((doc) => {
          const docRef = db.collection(collectionName).doc(doc.id);
          batch.set(docRef, defaultData);
        });

        await batch.commit();
        console.log("Database successfully reset");
      } catch (error) {
        console.error("Error resetting the database: ", error);
      }
    });
