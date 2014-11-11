package code.metadata.counterparties


import code.util.Helper
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import code.model.{UserId, ViewId, GeoTag}
//TODO: this should be private
class Metadata private() extends MongoRecord[Metadata] with ObjectIdPk[Metadata] {
  import net.liftweb.mongodb.record.field.BsonRecordField
  import java.util.Date

  def meta = Metadata

  //originalPartyBankId and originalPartyAccountId are used to identify the account
  //which has the counterparty this metadata is associated with
  object originalPartyBankId extends StringField(this, 100)
  object originalPartyAccountId extends StringField(this, 100)

  object holder extends StringField(this, 255)
  object accountNumber extends  StringField(this, 100)
  object publicAlias extends StringField(this, 100)
  object privateAlias extends StringField(this, 100)
  object moreInfo extends StringField(this, 100)
  object url extends StringField(this, 100)
  object imageUrl extends StringField(this, 100)
  object openCorporatesUrl extends StringField(this, 100) {
    override def optional_? = true
  }
  object corporateLocation extends BsonRecordField(this, OBPGeoTag)
  object physicalLocation extends BsonRecordField(this, OBPGeoTag)

  def addCorporateLocation(userId: UserId, viewId : ViewId, datePosted : Date, longitude : Double, latitude : Double) : Boolean = {
    val newTag = OBPGeoTag.createRecord.
      userId(userId.value).
      forView(viewId.value).
      date(datePosted).
      geoLongitude(longitude).
      geoLatitude(latitude)
    corporateLocation(newTag).save
    true
  }

  def deleteCorporateLocation : Boolean = {
    corporateLocation.clear
    this.save
    true
  }

  def addPhysicalLocation(userId: UserId, viewId : ViewId, datePosted : Date, longitude : Double, latitude : Double) : Boolean = {
    val newTag = OBPGeoTag.createRecord.
      userId(userId.value).
      forView(viewId.value).
      date(datePosted).
      geoLongitude(longitude).
      geoLatitude(latitude)
    physicalLocation(newTag).save
    true
  }

  def deletePhysicalLocation : Boolean = {
    physicalLocation.clear
    this.save
    true
  }

}

//TODO: this should be private
object Metadata extends Metadata with MongoMetaRecord[Metadata]

class OBPGeoTag private() extends BsonRecord[OBPGeoTag] with GeoTag {
  import code.model.User
  import net.liftweb.record.field.{DoubleField, LongField}
  import net.liftweb.mongodb.record.field.DateField

  def meta = OBPGeoTag

  //These fields are used to link this to its transaction
  object transactionId extends StringField(this, 255)
  object accountId extends StringField(this, 255)
  object bankId extends StringField(this, 255)

  object userId extends StringField(this,255)

  object forView extends StringField(this, 255)

  object date extends DateField(this)

  object geoLongitude extends DoubleField(this,0)
  object geoLatitude extends DoubleField(this,0)

  def datePosted = date.get
  def postedBy = User.findByApiId(userId.get)
  def viewId = ViewId(forView.get)
  def longitude = geoLongitude.get
  def latitude = geoLatitude.get

}
//TODO: this should be private
object OBPGeoTag extends OBPGeoTag with BsonMetaRecord[OBPGeoTag]