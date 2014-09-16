package code.metadata.narrative

import code.model.{TransactionId, AccountId, BankId}
import net.liftweb.common.Full
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import com.mongodb.{QueryBuilder, DBObject}

private object MongoTransactionNarrative extends Narrative {

  def getNarrative(bankId: BankId, accountId: AccountId, transactionId: TransactionId)() : String = {
    OBPNarrative.find(OBPNarrative.getFindQuery(bankId, accountId, transactionId)) match {
      case Full(n) => n.narrative.get
      case _ => ""
    }
  }

  def setNarrative(bankId: BankId, accountId: AccountId, transactionId: TransactionId)(narrative: String) : Unit = {

    val findQuery = OBPNarrative.getFindQuery(bankId, accountId, transactionId)

    if(narrative.isEmpty) {
      //if we're setting the value of the narrative to "" then we can just delete it

      //use delete with find query to avoid concurrency issues
      OBPNarrative.delete(findQuery)
    } else {

      val newNarrative = OBPNarrative.createRecord.
        transactionId(transactionId.value).
        accountId(accountId.value).
        bankId(bankId.value).
        narrative(narrative)

      //use an upsert to avoid concurrency issues
      OBPNarrative.upsert(findQuery, newNarrative.asDBObject)
    }

    //we don't have any useful information here so just assume it worked
    Full()
  }

}

private class OBPNarrative private() extends MongoRecord[OBPNarrative] with ObjectIdPk[OBPNarrative] {

  def meta = OBPNarrative

  //These fields are used to link this to its transaction
  object transactionId extends StringField(this, 255)
  object accountId extends StringField(this, 255)
  object bankId extends StringField(this, 255)

  object narrative extends StringField(this, 255)
}

private object OBPNarrative extends OBPNarrative with MongoMetaRecord[OBPNarrative] {
  def getFindQuery(bankId : BankId, accountId : AccountId, transactionId : TransactionId) : DBObject = {
    QueryBuilder.start("bankId").is(bankId.value).put("accountId").is(accountId.value).put("transactionId").is(transactionId.value).get
  }
}
