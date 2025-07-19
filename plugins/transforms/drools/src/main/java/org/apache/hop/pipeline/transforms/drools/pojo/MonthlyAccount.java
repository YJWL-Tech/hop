package org.apache.hop.pipeline.transforms.drools.pojo;

public class MonthlyAccount {

  private Integer xuhao; // 序号
  private String qijianMingcheng; // 期间名称
  private String mingxiKemuDuan; // 明细科目段
  private String mingxiKemuDuan1; // 明细科目段_1
  private String zimuDuan; // 子目段
  private String zimuDuanShuoming; // 子目段说明
  private String shichangDuan; // 市场段
  private String shichangDuanShuoming; // 市场段说明
  private String chanpinDuan; // 产品段
  private String chanpinDuanShuoming; // 产品段说明
  private String yewuHuodongDuan; // 业务活动段
  private String yewuHuodongDuanShuoming; // 业务活动段段说明
  private String chengbenZhongxinDuan; // 成本中心段
  private String chengbenZhongxinDuanShuoming; // 成本中心段说明
  private Double qichuYue; // 期初余额
  private Double jiexiang; // 借项
  private Double daixiang; // 贷项
  private Double benqiFasheng; // 本期发生
  private Double bennianFashengELeiji; // 本年发生额累计
  private Double qimoYue; // 期末余额
  private String accountGroup;
  private String costCenterGroup;

  public String getAccountGroup() {
    return accountGroup;
  }

  public String getCostCenterGroup() {
    return costCenterGroup;
  }

  public void setAccountGroup(String accountGroup) {
    this.accountGroup = accountGroup;
  }

  public void setCostCenterGroup(String costCenterGroup) {
    this.costCenterGroup = costCenterGroup;
  }

  public Integer getXuhao() {
    return xuhao;
  }

  public void setXuhao(Integer xuhao) {
    this.xuhao = xuhao;
  }

  public String getQijianMingcheng() {
    return qijianMingcheng;
  }

  public void setQijianMingcheng(String qijianMingcheng) {
    this.qijianMingcheng = qijianMingcheng;
  }

  public String getMingxiKemuDuan() {
    return mingxiKemuDuan;
  }

  public void setMingxiKemuDuan(String mingxiKemuDuan) {
    this.mingxiKemuDuan = mingxiKemuDuan;
  }

  public String getMingxiKemuDuan1() {
    return mingxiKemuDuan1;
  }

  public void setMingxiKemuDuan1(String mingxiKemuDuan1) {
    this.mingxiKemuDuan1 = mingxiKemuDuan1;
  }

  public String getZimuDuan() {
    return zimuDuan;
  }

  public void setZimuDuan(String zimuDuan) {
    this.zimuDuan = zimuDuan;
  }

  public String getZimuDuanShuoming() {
    return zimuDuanShuoming;
  }

  public void setZimuDuanShuoming(String zimuDuanShuoming) {
    this.zimuDuanShuoming = zimuDuanShuoming;
  }

  public String getShichangDuan() {
    return shichangDuan;
  }

  public void setShichangDuan(String shichangDuan) {
    this.shichangDuan = shichangDuan;
  }

  public String getShichangDuanShuoming() {
    return shichangDuanShuoming;
  }

  public void setShichangDuanShuoming(String shichangDuanShuoming) {
    this.shichangDuanShuoming = shichangDuanShuoming;
  }

  public String getChanpinDuan() {
    return chanpinDuan;
  }

  public void setChanpinDuan(String chanpinDuan) {
    this.chanpinDuan = chanpinDuan;
  }

  public String getChanpinDuanShuoming() {
    return chanpinDuanShuoming;
  }

  public void setChanpinDuanShuoming(String chanpinDuanShuoming) {
    this.chanpinDuanShuoming = chanpinDuanShuoming;
  }

  public String getYewuHuodongDuan() {
    return yewuHuodongDuan;
  }

  public void setYewuHuodongDuan(String yewuHuodongDuan) {
    this.yewuHuodongDuan = yewuHuodongDuan;
  }

  public String getYewuHuodongDuanShuoming() {
    return yewuHuodongDuanShuoming;
  }

  public void setYewuHuodongDuanShuoming(String yewuHuodongDuanShuoming) {
    this.yewuHuodongDuanShuoming = yewuHuodongDuanShuoming;
  }

  public String getChengbenZhongxinDuan() {
    return chengbenZhongxinDuan;
  }

  public void setChengbenZhongxinDuan(String chengbenZhongxinDuan) {
    this.chengbenZhongxinDuan = chengbenZhongxinDuan;
  }

  public String getChengbenZhongxinDuanShuoming() {
    return chengbenZhongxinDuanShuoming;
  }

  public void setChengbenZhongxinDuanShuoming(String chengbenZhongxinDuanShuoming) {
    this.chengbenZhongxinDuanShuoming = chengbenZhongxinDuanShuoming;
  }

  public Double getQichuYue() {
    return qichuYue;
  }

  public void setQichuYue(Double qichuYue) {
    this.qichuYue = qichuYue;
  }

  public Double getJiexiang() {
    return jiexiang;
  }

  public void setJiexiang(Double jiexiang) {
    this.jiexiang = jiexiang;
  }

  public Double getDaixiang() {
    return daixiang;
  }

  public void setDaixiang(Double daixiang) {
    this.daixiang = daixiang;
  }

  public Double getBenqiFasheng() {
    return benqiFasheng;
  }

  public void setBenqiFasheng(Double benqiFasheng) {
    this.benqiFasheng = benqiFasheng;
  }

  public Double getBennianFashengELeiji() {
    return bennianFashengELeiji;
  }

  public void setBennianFashengELeiji(Double bennianFashengELeiji) {
    this.bennianFashengELeiji = bennianFashengELeiji;
  }

  public Double getQimoYue() {
    return qimoYue;
  }

  public void setQimoYue(Double qimoYue) {
    this.qimoYue = qimoYue;
  }

  @Override
  public String toString() {
    return "MonthlyAccount{"
        + "xuhao="
        + xuhao
        + ", qijianMingcheng='"
        + qijianMingcheng
        + '\''
        + ", mingxiKemuDuan='"
        + mingxiKemuDuan
        + '\''
        + ", mingxiKemuDuan1='"
        + mingxiKemuDuan1
        + '\''
        + ", zimuDuan='"
        + zimuDuan
        + '\''
        + ", zimuDuanShuoming='"
        + zimuDuanShuoming
        + '\''
        + ", shichangDuan='"
        + shichangDuan
        + '\''
        + ", shichangDuanShuoming='"
        + shichangDuanShuoming
        + '\''
        + ", chanpinDuan='"
        + chanpinDuan
        + '\''
        + ", chanpinDuanShuoming='"
        + chanpinDuanShuoming
        + '\''
        + ", yewuHuodongDuan='"
        + yewuHuodongDuan
        + '\''
        + ", yewuHuodongDuanShuoming='"
        + yewuHuodongDuanShuoming
        + '\''
        + ", chengbenZhongxinDuan='"
        + chengbenZhongxinDuan
        + '\''
        + ", chengbenZhongxinDuanShuoming='"
        + chengbenZhongxinDuanShuoming
        + '\''
        + ", qichuYue="
        + qichuYue
        + ", jiexiang="
        + jiexiang
        + ", daixiang="
        + daixiang
        + ", benqiFasheng="
        + benqiFasheng
        + ", bennianFashengELeiji="
        + bennianFashengELeiji
        + ", qimoYue="
        + qimoYue
        + ", accountGroup='"
        + accountGroup
        + '\''
        + ", costCenterGroup='"
        + costCenterGroup
        + '\''
        + '}';
  }
}
