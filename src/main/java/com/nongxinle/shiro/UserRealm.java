/**
 * com.nongxinle.shiro class
 *
 * @Author: peiyi li
 * @Date: 2019-07-12 21:48
 */

package com.nongxinle.shiro;

import com.nongxinle.entity.SysMenuEntity;
import com.nongxinle.entity.SysUserEntity;
import com.nongxinle.service.SysMenuService;
import com.nongxinle.service.SysUserService;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 *@author lpy
 *@date 2019-07-12 21:48
 */


    /**
     * 认证
     *
     */
    public class UserRealm extends AuthorizingRealm {
        @Autowired
        private SysUserService sysUserService;
        @Autowired
        private SysMenuService sysMenuService;

        /**
         * 授权(验证权限时调用)
         */
        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
            SysUserEntity user = (SysUserEntity)principals.getPrimaryPrincipal();
            Long userId = user.getUserId();


            System.out.println("quanxian!!!!!!");
            List<String> permsList = null;

            //系统管理员，拥有最高权限
            if(userId == 1){
                List<SysMenuEntity> menuList = sysMenuService.queryList(new HashMap<String, Object>());
                permsList = new ArrayList<>(menuList.size());
                for(SysMenuEntity menu : menuList){
                    permsList.add(menu.getPerms());
                }
            }else{
                permsList = sysUserService.queryAllPerms(userId);
            }

            //用户权限列表
            Set<String> permsSet = new HashSet<String>();
            for(String perms : permsList){
                if(StringUtils.isBlank(perms)){
                    continue;
                }
                permsSet.addAll(Arrays.asList(perms.trim().split(",")));
            }

            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            info.setStringPermissions(permsSet);
            return info;
        }

        /**
         * 认证(登录时调用)
         */
        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
            String username = (String) token.getPrincipal();
            String password = new String((char[]) token.getCredentials());

            System.out.println("doGetAuthenticationInfo...........////////???????");

            //查询用户信息
            SysUserEntity user = sysUserService.queryByUserName(username);

            //账号不存在
            if(user == null) {
                throw new UnknownAccountException("账号或密码不正确");
            }
            //密码错误
            System.out.println("password=====" + password);
            System.out.println("user.getPassword()=====" + user.getPassword());
            if(!password.equals(user.getPassword())) {
                throw new IncorrectCredentialsException("账号或密码不正确");
            }

            //账号锁定
            if(user.getStatus() == 0){
                throw new LockedAccountException("账号已被锁定,请联系管理员");
            }

            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, password, getName());
            System.out.println("info=====" + info);
            return info;
        }
}
