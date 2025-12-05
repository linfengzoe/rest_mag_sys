import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    cart: JSON.parse(localStorage.getItem('cart') || '[]')
  },
  getters: {
    isAuthenticated: state => !!state.token,
    userRole: state => state.userInfo.role || '',
    username: state => state.userInfo.username || '',
    avatar: state => state.userInfo.avatar || '',
    userId: state => state.userInfo.id || '',
    cartItems: state => state.cart,
    cartCount: state => state.cart.reduce((total, item) => total + item.quantity, 0)
  },
  mutations: {
    setToken(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    setUserInfo(state, userInfo) {
      state.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    clearAuth(state) {
      state.token = ''
      state.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    addToCart(state, item) {
      const existingItem = state.cart.find(i => i.id === item.id)
      if (existingItem) {
        existingItem.quantity += item.quantity
      } else {
        state.cart.push(item)
      }
      localStorage.setItem('cart', JSON.stringify(state.cart))
    },
    updateCartItem(state, { id, quantity }) {
      const item = state.cart.find(i => i.id === id)
      if (item) {
        item.quantity = quantity
        localStorage.setItem('cart', JSON.stringify(state.cart))
      }
    },
    removeCartItem(state, id) {
      state.cart = state.cart.filter(item => item.id !== id)
      localStorage.setItem('cart', JSON.stringify(state.cart))
    },
    clearCart(state) {
      state.cart = []
      localStorage.setItem('cart', JSON.stringify(state.cart))
    }
  },
  actions: {
    login({ commit }, { token, userInfo }) {
      commit('setToken', token)
      commit('setUserInfo', userInfo)
    },
    logout({ commit }) {
      commit('clearAuth')
      commit('clearCart')
    }
  },
  modules: {
  }
}) 