package com.shcd.footandroidbar.entity;

public class TouchGUIVO extends BaseEntity{

	/***/
	private static final long serialVersionUID = -2615276188797253164L;

	private CompanyInfo companyInfo;

	public CompanyInfo getCompanyInfo() {
		if (companyInfo==null) {
			companyInfo=new CompanyInfo();
		}
		return companyInfo;
	}

	public void setCompanyInfo(CompanyInfo companyInfo) {
		this.companyInfo = companyInfo;
	}
}
