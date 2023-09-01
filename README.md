# Swagger 注解替换为文档注释工具类

### Swagger 注解替换为文档注释工具类

此项目主要是实现了将 Swagger 注解替换为文档注释的功能，目前支持的注解有： @ApiModelPropert、@ApiModel、@ApiOperation、@ApiResponse、@ApiImplicitParam、@ApiImplicitParams


### 执行效果
原始代码如下：
```java
/*
 * Copyright (C), 2008-2023
 */
package com.paraview.idm.client.permission.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zy
 * @since 2023-08-24 10:21
 */
@Data
@ApiModel(value = "AppAccountBindGroupDTO对象", description = "获取已绑定/未绑定帐号列表")
public class AppAccountBindGroupDTO extends AppAccountBase {

    @ApiModelProperty("组id，多个以逗号分隔")
    private String groupIds;
}

```


替换后的代码如下：
```java
/*
 * Copyright (C), 2008-2023
 */
package com.paraview.idm.client.permission.request;

import lombok.Data;

/**
 * @author zy
 * @since 2023-08-24 10:21
 */
@Data
/**
 * 获取已绑定/未绑定帐号列表
 */
public class AppAccountBindGroupDTO extends AppAccountBase {

    /**
     * 组id，多个以逗号分隔
     */
    private String groupIds;
}

```
