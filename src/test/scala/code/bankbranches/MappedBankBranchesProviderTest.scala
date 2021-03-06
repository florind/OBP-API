package code.bankbranches

import code.api.test.ServerSetup
import code.model.BankId
import net.liftweb.mapper.By

class MappedBankBranchesProviderTest extends ServerSetup {

  private def delete(): Unit = {
    MappedBankBranch.bulkDelete_!!()
    MappedDataLicense.bulkDelete_!!()
  }

  override def beforeAll() = {
    super.beforeAll()
    delete()
  }

  override def afterEach() = {
    super.afterEach()
    delete()
  }

  def defaultSetup() =
    new {
      val bankIdWithLicenseAndData = "some-bank"
      val bankIdWithNoLicense = "unlicensed-bank"

      val license = MappedDataLicense.create
        .mBankId(bankIdWithLicenseAndData)
        .mName("some-license")
        .mUrl("http://www.example.com/license").saveMe()

      val unlicensedBranch = MappedBankBranch.create
        .mBankId(bankIdWithNoLicense)
        .mName("unlicensed")
        .mBranchId("unlicensed")
        .mCountryCode("es")
        .mPostCode("4444")
        .mLine1("a4")
        .mLine2("b4")
        .mLine3("c4")
        .mLine4("d4")
        .mLine5("e4").saveMe()

      val branch1 = MappedBankBranch.create
        .mBankId(bankIdWithLicenseAndData)
        .mName("branch 1")
        .mBranchId("branch1")
        .mCountryCode("de")
        .mPostCode("123213213")
        .mLine1("a")
        .mLine2("b")
        .mLine3("c")
        .mLine4("d")
        .mLine5("e").saveMe()

      val branch2 = MappedBankBranch.create
        .mBankId(bankIdWithLicenseAndData)
        .mName("branch 2")
        .mBranchId("branch2")
        .mCountryCode("fr")
        .mPostCode("898989")
        .mLine1("a2")
        .mLine2("b2")
        .mLine3("c2")
        .mLine4("d2")
        .mLine5("e2").saveMe()
    }

  feature("MappedBankBranchesProvider") {

    scenario("We try to get branch data for a bank which does not have a data license set") {
      val fixture = defaultSetup()

      Given("The bank in question has no data license")
      MappedDataLicense.count(By(MappedDataLicense.mBankId, fixture.bankIdWithNoLicense)) should equal(0)

      And("The bank in question has branches")
      MappedBankBranch.find(By(MappedBankBranch.mBankId, fixture.bankIdWithNoLicense)).isDefined should equal(true)

      When("We try to get the branch data for that bank")
      val branchData = MappedBankBranchesProvider.getBranches(BankId(fixture.bankIdWithNoLicense))

      Then("We should get an empty option")
      branchData should equal(None)

    }

    scenario("We try to get branch data for a bank which does have a data license set") {
      val fixture = defaultSetup()
      val expectedBranches = Set(fixture.branch1, fixture.branch2)
      Given("We have a data license and branches for a bank")
      MappedDataLicense.count(By(MappedDataLicense.mBankId, fixture.bankIdWithLicenseAndData)) should equal(1)
      MappedBankBranch.findAll(By(MappedBankBranch.mBankId, fixture.bankIdWithLicenseAndData)).toSet should equal(expectedBranches)

      When("We try to get the branch data for that bank")
      val branchDataOpt = MappedBankBranchesProvider.getBranches(BankId(fixture.bankIdWithLicenseAndData))

      Then("We should get back the data license and the branches")
      branchDataOpt.isDefined should equal(true)
      val branchData = branchDataOpt.get

      branchData.license should equal(fixture.license)
      branchData.branches.toSet should equal(expectedBranches)
    }

    scenario("We try to get branch data for a bank with a data license, but no branches") {

      Given("We have a data license for a bank, but no branches")

      val bankWithNoBranches = "bank-with-no-branches"
      val license = MappedDataLicense.create
        .mBankId(bankWithNoBranches)
        .mName("some-license")
        .mUrl("http://www.example.com/license").saveMe()

      MappedBankBranch.find(By(MappedBankBranch.mBankId, bankWithNoBranches)).isDefined should equal(false)

      When("We try to get the branch data for that bank")
      val branchDataOpt = MappedBankBranchesProvider.getBranches(BankId(bankWithNoBranches))

      Then("We should get back the data license, and a list branches of size 0")
      branchDataOpt.isDefined should equal(true)
      val branchData = branchDataOpt.get

      branchData.license should equal(license)
      branchData.branches should equal(Nil)

    }

  }
}
