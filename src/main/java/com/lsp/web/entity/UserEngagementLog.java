package com.lsp.web.entity;

import com.lsp.web.genericentity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_user_engagement_log", indexes = {@Index(name = "idx_mobile", columnList = "mobileNumber")})
public class UserEngagementLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userinfo_id")
    private UserInfo user;

    @Column(name = "mobileNumber", length = 15)
    private String mobileNumber;
    
    public Integer getSubAgentId() {
		return subAgentId;
	}
	
	@Column(name = "agent_id", length = 20)
	 private Integer agentId;//dsa
	    
   @Column(name = "agent", length = 30)
   private String agent;//source
   
	public void setSubAgentId(Integer subAgentId) {
		this.subAgentId = subAgentId;
	}


	public Integer getAgentId() {
		return agentId;
	}

	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	@Column(name = "sub_agent_id", length = 30)
    private Integer subAgentId; // sub_dsa

    
    @Column(name = "click_id", length = 50)
    private String clickId; //clickid 

    
    @Column(name = "last_attribution_time")
    private LocalDateTime lastAttributionTime;//last attribution time column

    @Column(name = "sub_agent", length = 50)//sub dsa
    private String sub_agent;

    public String getClickId() {
		return clickId;
	}
    @Column(name = "registertime")
    private LocalDateTime registerTime;
    
    @PrePersist
    public void prePersist() {
        if (this.registerTime == null) {
            this.registerTime = super.getCreateTime(); // mirror createTime from BaseEntity
        }
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }


	@Column(name = "source", length = 50)
    private String source; //App Source

    @Column(name = "web_source", length = 50)
    private String webSource; //Web Source
    
    
    @Column(name = "campaign", length = 500)
    private String campaign;  //campaign

    @Column(name = "channel", length = 50)
    private String channel; //channel

    public UserEngagementLog() {
        // Set lastAttributionTime default to now
        this.lastAttributionTime = LocalDateTime.now();
    }

    // Getters and Setters
    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
	public void setClickId(String clickId) {
		this.clickId = clickId;
	}

	public LocalDateTime getLastAttributionTime() {
		return lastAttributionTime;
	}

	public void setLastAttributionTime(LocalDateTime lastAttributionTime) {
		this.lastAttributionTime = lastAttributionTime;
	}

	public String getSub_agent() {
		return sub_agent;
	}

	public void setSub_agent(String sub_agent) {
		this.sub_agent = sub_agent;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getWebSource() {
		return webSource;
	}

	public void setWebSource(String webSource) {
		this.webSource = webSource;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}




   
}
