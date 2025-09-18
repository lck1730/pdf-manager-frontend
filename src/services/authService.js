// authService.js
class AuthService {
  // 存储访问令牌
  setAccessToken(token) {
    localStorage.setItem('accessToken', token);
  }

  // 存储刷新令牌
  setRefreshToken(token) {
    localStorage.setItem('refreshToken', token);
  }

  // 获取访问令牌
  getAccessToken() {
    return localStorage.getItem('accessToken');
  }

  // 获取刷新令牌
  getRefreshToken() {
    return localStorage.getItem('refreshToken');
  }

  // 清除令牌
  clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  // 检查用户是否已认证
  isAuthenticated() {
    return !!this.getAccessToken();
  }

  // 设置认证头
  setAuthHeader() {
    const token = this.getAccessToken();
    if (token) {
      return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      };
    }
    return {
      'Content-Type': 'application/json'
    };
  }
}

export default new AuthService();