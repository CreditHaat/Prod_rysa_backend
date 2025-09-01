package com.lsp.web.util;

public class ApiResponse {
	
	    private int code;
	    private String msg;
	    private Object obj;
	    private String stgOneHitId;
	    private String stgTwoHitId;
	    private String otpGenerationStatus;
	    
	    public ApiResponse() {
	        this.code = 1;
	        this.msg = "Success";
	    }
	    
	    public ApiResponse(int code, String msg) {
	        this.code = code;
	        this.msg = msg;
	    }
	    
		public ApiResponse(int code, String msg, Object obj, String stgOneHitId, String stgTwoHitId,
				String otpGenerationStatus) {
			super();
			this.code = code;
			this.msg = msg;
			this.obj = obj;
			this.stgOneHitId = stgOneHitId;
			this.stgTwoHitId = stgTwoHitId;
			this.otpGenerationStatus = otpGenerationStatus;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public Object getObj() {
			return obj;
		}

		public void setObj(Object obj) {
			this.obj = obj;
		}

		public String getStgOneHitId() {
			return stgOneHitId;
		}

		public void setStgOneHitId(String stgOneHitId) {
			this.stgOneHitId = stgOneHitId;
		}

		public String getStgTwoHitId() {
			return stgTwoHitId;
		}

		public void setStgTwoHitId(String stgTwoHitId) {
			this.stgTwoHitId = stgTwoHitId;
		}

		public String getOtpGenerationStatus() {
			return otpGenerationStatus;
		}

		public void setOtpGenerationStatus(String otpGenerationStatus) {
			this.otpGenerationStatus = otpGenerationStatus;
		}

}

