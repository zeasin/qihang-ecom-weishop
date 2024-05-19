package cn.qihangerp.oms.security;

import cn.qihangerp.common.redis.RedisCache;
import cn.qihangerp.common.utils.AddressUtils;
import cn.qihangerp.common.utils.IdUtils;
import cn.qihangerp.common.utils.IpUtils;
import cn.qihangerp.common.utils.ServletUtils;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * token验证处理
 *
 * @author qihang
 */
@Component
public class TokenService
{
    // 令牌自定义标识
//    @Value("${token.header:'Authorization'}")
//    private String header;

    // 令牌秘钥
    @Value("${token.secret:'abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkrstuvwxyzabcdefghijklmnopqrstuvwxyz'}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime:30}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;

    /**
     * 令牌前缀
     */
    public static final String LOGIN_CLIENT_KEY = "login_client_key";

    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_dist_tokens:";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginDistributor getLoginDistributor(HttpServletRequest request)
    {
        // 获取请求携带的令牌
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(LOGIN_CLIENT_KEY);
                String userKey = getTokenKey(uuid);
                LoginDistributor user = redisCache.getCacheObject(userKey);
                return user;
            }
            catch (Exception e)
            {
            }
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginDistributor(LoginDistributor loginDistributor)
    {
        if (loginDistributor != null && StringUtils.isNotEmpty(loginDistributor.getToken()))
        {
            refreshToken(loginDistributor);
        }
    }

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     * 创建令牌
     *
     * @param loginDistributor 用户信息
     * @return 令牌
     */
    public String createToken(LoginDistributor loginDistributor)
    {
        String token = IdUtils.fastUUID();
        loginDistributor.setToken(token);
        setUserAgent(loginDistributor);
        refreshToken(loginDistributor);

        Map<String, Object> claims = new HashMap<>();
        claims.put(LOGIN_CLIENT_KEY, token);
        return createToken(claims);
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param loginDistributor
     * @return 令牌
     */
    public void verifyToken(LoginDistributor loginDistributor)
    {
        long expireTime = loginDistributor.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN)
        {
            refreshToken(loginDistributor);
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginDistributor 登录信息
     */
    public void refreshToken(LoginDistributor loginDistributor)
    {
        loginDistributor.setLoginTime(System.currentTimeMillis());
        loginDistributor.setExpireTime(loginDistributor.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = getTokenKey(loginDistributor.getToken());
        redisCache.setCacheObject(userKey, loginDistributor, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 设置用户代理信息
     *
     * @param loginDistributor 登录信息
     */
    public void setUserAgent(LoginDistributor loginDistributor)
    {
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr();
        loginDistributor.setIpaddr(ip);
        loginDistributor.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginDistributor.setBrowser(userAgent.getBrowser().getName());
        loginDistributor.setOs(userAgent.getOperatingSystem().getName());
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims)
    {
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        return token;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token)
    {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token)
    {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    private String getToken(HttpServletRequest request)
    {
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(token) && token.startsWith(TOKEN_PREFIX))
        {
            token = token.replace(TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid)
    {
        return LOGIN_TOKEN_KEY + uuid;
    }
}