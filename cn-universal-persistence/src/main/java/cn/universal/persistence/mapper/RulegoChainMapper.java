/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.mapper;

import cn.universal.persistence.entity.RulegoChain;
import cn.universal.persistence.entity.bo.RulegoChainBO;
import cn.universal.persistence.entity.vo.RulegoChainVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * rulego规则链Mapper接口
 *
 * @author Aleo
 * @since 2025/01/15
 */
public interface RulegoChainMapper extends Mapper<RulegoChain> {

  /**
   * 根据条件查询规则链列表
   *
   * @param bo 查询条件
   * @return 规则链列表
   */
  List<RulegoChainVO> selectRulegoChainListByBo(@Param("bo") RulegoChainBO bo);

  /**
   * 根据rulegoId查询规则链
   *
   * @param rulegoId rulego规则链ID
   * @return 规则链信息
   */
  RulegoChain selectByRulegoId(@Param("rulegoId") String rulegoId);

  /**
   * 根据创建人查询规则链列表
   *
   * @param creatorId 创建人ID
   * @return 规则链列表
   */
  List<RulegoChain> selectByCreatorId(@Param("creatorId") String creatorId);

  /**
   * 更新DSL内容
   *
   * @param rulegoId rulego规则链ID
   * @param dslContent DSL内容
   * @return 更新行数
   */
  int updateDslContent(@Param("rulegoId") String rulegoId, @Param("dslContent") String dslContent);
}
