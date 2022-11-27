package com.zt.rpc.dto;

import com.alibaba.fastjson2.JSON;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class RegisterInfo implements Serializable {

    private String ip;

    private Integer port;

    private List<String> services;

    public String servicesToString() {
        return JSON.toJSONString(services);
    }

    public void stringToServices(String servicesString) {
        List<?> list = JSON.parseObject(servicesString, this.services.getClass());
        this.services = list.stream().map(Object::toString).collect(Collectors.toList());
    }

}
