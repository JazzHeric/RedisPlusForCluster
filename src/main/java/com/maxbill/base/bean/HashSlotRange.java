package com.maxbill.base.bean;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * @author: chi.zhang
 * @date: created in 2021/2/1 15:32
 * @description:
 */
public class HashSlotRange implements Serializable {

    private static final long serialVersionUID = -3233624765332018870L;

    private Long minSlotNo;

    private Long maxSlotNo;


    public HashSlotRange() {
    }

    private HashSlotRange(Builder builder) {
        this.minSlotNo = builder.minSlotNo;
        this.maxSlotNo = builder.maxSlotNo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getMinSlotNo() {
        return minSlotNo;
    }

    public void setMinSlotNo(Long minSlotNo) {
        this.minSlotNo = minSlotNo;
    }

    public Long getMaxSlotNo() {
        return maxSlotNo;
    }

    public void setMaxSlotNo(Long maxSlotNo) {
        this.maxSlotNo = maxSlotNo;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HashSlotRange.class.getSimpleName() + "[", "]")
                .add("minSlotNo=" + minSlotNo)
                .add("maxSlotNo=" + maxSlotNo)
                .toString();
    }


    public static final class Builder {
        private Long minSlotNo;
        private Long maxSlotNo;

        private Builder() {
        }

        public HashSlotRange build() {
            return new HashSlotRange(this);
        }

        public Builder minSlotNo(Long minSlotNo) {
            this.minSlotNo = minSlotNo;
            return this;
        }

        public Builder maxSlotNo(Long maxSlotNo) {
            this.maxSlotNo = maxSlotNo;
            return this;
        }
    }
}
