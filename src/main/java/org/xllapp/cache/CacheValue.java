package org.xllapp.cache;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 保存到缓存中的实体.
 * 
 * @author dylan.chen Sep 7, 2013
 * 
 */
public class CacheValue implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 缓存的内容
	 */
	private Serializable object;

	/**
	 * 缓存的创建时间
	 */
	private Date createDate;

	public CacheValue(Serializable object) {
		this(object, new Date());
	}

	public CacheValue(Serializable object, Date createDate) {
		this.object = object;
		this.createDate = createDate;
		if (!isValid()) {
			throw new RuntimeException("invalid cache value[createDate=" + createDate + ",object=" + object + "]");
		}
	}

	/**
	 * 缓存是否有效
	 * 
	 * 注：特殊情况下会出现:获取回来的CacheValue对象的createDate属性为null的情况，初步考虑是由于缓存数据被破坏了.
	 * 
	 * @return 有效的缓存返回:true,无效的缓存返回:false
	 */
	public boolean isValid() {
		return null != this.createDate;
	}

	public Serializable getObject() {
		return this.object;
	}

	public void setObject(Serializable object) {
		this.object = object;
	}

	public Date getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(Date createDate) {
		if (null != createDate) {
			this.createDate = createDate;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.createDate == null ? 0 : this.createDate.hashCode());
		result = prime * result + (this.object == null ? 0 : this.object.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CacheValue other = (CacheValue) obj;
		if (this.createDate == null) {
			if (other.createDate != null) {
				return false;
			}
		} else if (!this.createDate.equals(other.createDate)) {
			return false;
		}
		if (this.object == null) {
			if (other.object != null) {
				return false;
			}
		} else if (!this.object.equals(other.object)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CacheValue [object=");
		builder.append(this.object);
		builder.append(", createDate=");
		builder.append(DateFormatUtils.format(this.createDate, "yyyy-MM-dd HH:mm:ss"));
		builder.append("]");
		return builder.toString();
	}

}