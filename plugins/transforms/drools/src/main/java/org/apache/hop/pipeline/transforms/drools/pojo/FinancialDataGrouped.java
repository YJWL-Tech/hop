package org.apache.hop.pipeline.transforms.drools.pojo;

import java.math.BigDecimal;

public class FinancialDataGrouped {

  private Long id; // 主键ID
  private String fileId; // 文件ID
  private Long tenantId; // 租户ID
  private Integer sequenceNumber; // 序号
  private String periodName; // 期间名称
  private String detailSubjectSegment; // 明细科目段
  private String detailSubjectName; // 明细科目段名称
  private String subSubjectSegment; // 子目段
  private String subSubjectDescription; // 子目段说明
  private String marketSegment; // 市场段
  private String marketSegmentDescription; // 市场段说明
  private String productSegment; // 产品段
  private String productSegmentDescription; // 产品段说明
  private String businessActivitySegment; // 业务活动段
  private String businessActivityDescription; // 业务活动段说明
  private String costCenterSegment; // 成本中心段
  private String costCenterDescription; // 成本中心段说明
  private BigDecimal openingBalance; // 期初余额
  private BigDecimal debitAmount; // 借项
  private BigDecimal creditAmount; // 贷项
  private BigDecimal currentPeriodAmount; // 本期发生
  private BigDecimal yearToDateAmount; // 本年发生额累计
  private BigDecimal closingBalance; // 期末余额
  private String accountingAccountGroup; // 会计科目组
  private String costCenterGroup; // 成本中心组

  // 默认构造函数
  public FinancialDataGrouped() {}

  // Getter和Setter方法
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public Long getTenantId() {
    return tenantId;
  }

  public void setTenantId(Long tenantId) {
    this.tenantId = tenantId;
  }

  public Integer getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Integer sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public String getPeriodName() {
    return periodName;
  }

  public void setPeriodName(String periodName) {
    this.periodName = periodName;
  }

  public String getDetailSubjectSegment() {
    return detailSubjectSegment;
  }

  public void setDetailSubjectSegment(String detailSubjectSegment) {
    this.detailSubjectSegment = detailSubjectSegment;
  }

  public String getDetailSubjectName() {
    return detailSubjectName;
  }

  public void setDetailSubjectName(String detailSubjectName) {
    this.detailSubjectName = detailSubjectName;
  }

  public String getSubSubjectSegment() {
    return subSubjectSegment;
  }

  public void setSubSubjectSegment(String subSubjectSegment) {
    this.subSubjectSegment = subSubjectSegment;
  }

  public String getSubSubjectDescription() {
    return subSubjectDescription;
  }

  public void setSubSubjectDescription(String subSubjectDescription) {
    this.subSubjectDescription = subSubjectDescription;
  }

  public String getMarketSegment() {
    return marketSegment;
  }

  public void setMarketSegment(String marketSegment) {
    this.marketSegment = marketSegment;
  }

  public String getMarketSegmentDescription() {
    return marketSegmentDescription;
  }

  public void setMarketSegmentDescription(String marketSegmentDescription) {
    this.marketSegmentDescription = marketSegmentDescription;
  }

  public String getProductSegment() {
    return productSegment;
  }

  public void setProductSegment(String productSegment) {
    this.productSegment = productSegment;
  }

  public String getProductSegmentDescription() {
    return productSegmentDescription;
  }

  public void setProductSegmentDescription(String productSegmentDescription) {
    this.productSegmentDescription = productSegmentDescription;
  }

  public String getBusinessActivitySegment() {
    return businessActivitySegment;
  }

  public void setBusinessActivitySegment(String businessActivitySegment) {
    this.businessActivitySegment = businessActivitySegment;
  }

  public String getBusinessActivityDescription() {
    return businessActivityDescription;
  }

  public void setBusinessActivityDescription(String businessActivityDescription) {
    this.businessActivityDescription = businessActivityDescription;
  }

  public String getCostCenterSegment() {
    return costCenterSegment;
  }

  public void setCostCenterSegment(String costCenterSegment) {
    this.costCenterSegment = costCenterSegment;
  }

  public String getCostCenterDescription() {
    return costCenterDescription;
  }

  public void setCostCenterDescription(String costCenterDescription) {
    this.costCenterDescription = costCenterDescription;
  }

  public BigDecimal getOpeningBalance() {
    return openingBalance;
  }

  public void setOpeningBalance(BigDecimal openingBalance) {
    this.openingBalance = openingBalance;
  }

  public BigDecimal getDebitAmount() {
    return debitAmount;
  }

  public void setDebitAmount(BigDecimal debitAmount) {
    this.debitAmount = debitAmount;
  }

  public BigDecimal getCreditAmount() {
    return creditAmount;
  }

  public void setCreditAmount(BigDecimal creditAmount) {
    this.creditAmount = creditAmount;
  }

  public BigDecimal getCurrentPeriodAmount() {
    return currentPeriodAmount;
  }

  public void setCurrentPeriodAmount(BigDecimal currentPeriodAmount) {
    this.currentPeriodAmount = currentPeriodAmount;
  }

  public BigDecimal getYearToDateAmount() {
    return yearToDateAmount;
  }

  public void setYearToDateAmount(BigDecimal yearToDateAmount) {
    this.yearToDateAmount = yearToDateAmount;
  }

  public BigDecimal getClosingBalance() {
    return closingBalance;
  }

  public void setClosingBalance(BigDecimal closingBalance) {
    this.closingBalance = closingBalance;
  }

  public String getAccountingAccountGroup() {
    return accountingAccountGroup;
  }

  public void setAccountingAccountGroup(String accountingAccountGroup) {
    this.accountingAccountGroup = accountingAccountGroup;
  }

  public String getCostCenterGroup() {
    return costCenterGroup;
  }

  public void setCostCenterGroup(String costCenterGroup) {
    this.costCenterGroup = costCenterGroup;
  }

  @Override
  public String toString() {
    return "FinancialDataGrouped{"
        + "id="
        + id
        + ", fileId='"
        + fileId
        + '\''
        + ", tenantId="
        + tenantId
        + ", sequenceNumber="
        + sequenceNumber
        + ", periodName='"
        + periodName
        + '\''
        + ", detailSubjectSegment='"
        + detailSubjectSegment
        + '\''
        + ", detailSubjectName='"
        + detailSubjectName
        + '\''
        + ", subSubjectSegment='"
        + subSubjectSegment
        + '\''
        + ", subSubjectDescription='"
        + subSubjectDescription
        + '\''
        + ", marketSegment='"
        + marketSegment
        + '\''
        + ", marketSegmentDescription='"
        + marketSegmentDescription
        + '\''
        + ", productSegment='"
        + productSegment
        + '\''
        + ", productSegmentDescription='"
        + productSegmentDescription
        + '\''
        + ", businessActivitySegment='"
        + businessActivitySegment
        + '\''
        + ", businessActivityDescription='"
        + businessActivityDescription
        + '\''
        + ", costCenterSegment='"
        + costCenterSegment
        + '\''
        + ", costCenterDescription='"
        + costCenterDescription
        + '\''
        + ", openingBalance="
        + openingBalance
        + ", debitAmount="
        + debitAmount
        + ", creditAmount="
        + creditAmount
        + ", currentPeriodAmount="
        + currentPeriodAmount
        + ", yearToDateAmount="
        + yearToDateAmount
        + ", closingBalance="
        + closingBalance
        + ", accountingAccountGroup='"
        + accountingAccountGroup
        + '\''
        + ", costCenterGroup='"
        + costCenterGroup
        + '\''
        + '}';
  }
}
