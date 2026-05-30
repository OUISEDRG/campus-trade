import { defineStore } from 'pinia'
import { useUserStore } from '../stores/user'

export const useChatStore = defineStore('chat', {
  state: () => ({
    unreadTotal: 0,
    socket: null,
    sessionList: [],
    activeSessionId: null,
    currentMessages: [],
    currentContextGoods: null
  }),
  actions: {
    initGlobalWebSocket(userId) {
      if (this.socket) {
        this.socket.close()
      }
      
      try {
        this.socket = new WebSocket(`ws://localhost:8080/ws/chat/${userId}`)
        
        this.socket.onopen = () => {
          // 连接成功，不输出日志
        }
        
        this.socket.onmessage = (event) => {
          try {
            const msg = JSON.parse(event.data)
            this.handleNewMessage(msg)
          } catch (e) {
            console.error('解析消息失败:', e)
          }
        }
        
        this.socket.onclose = () => {
          // 连接关闭，不输出日志
        }
        
        this.socket.onerror = (error) => {
          console.error('WebSocket错误:', error)
        }
      } catch (e) {
        console.error('WebSocket初始化失败:', e)
      }
    },
    
    handleNewMessage(msg) {
      const sessionIndex = this.sessionList.findIndex(
        s => s.targetUserId === msg.senderId || s.targetUserId === msg.receiverId
      )
      
      if (sessionIndex >= 0) {
        this.sessionList[sessionIndex].lastMessage = msg.content
        this.sessionList[sessionIndex].lastMessageTime = msg.createTime
        
        if (this.activeSessionId !== this.sessionList[sessionIndex].targetUserId) {
          this.sessionList[sessionIndex].unreadCount += 1
          this.unreadTotal += 1
        }
      } else {
        const targetUserId = msg.senderId === this.getCurrentUserId() ? msg.receiverId : msg.senderId
        this.sessionList.unshift({
          targetUserId,
          targetUserName: msg.senderName || '用户',
          lastMessage: msg.content,
          lastMessageTime: msg.createTime,
          unreadCount: 1,
          goodsId: msg.goodsId
        })
        this.unreadTotal += 1
      }
    },
    
    getCurrentUserId() {
      const userStore = useUserStore()
      return userStore.activeUserId
    },
    
    selectSession(session) {
      this.activeSessionId = session.targetUserId
      this.currentContextGoods = null
      this.currentMessages = []
      
      if (session.unreadCount > 0) {
        this.unreadTotal = Math.max(0, this.unreadTotal - session.unreadCount)
        session.unreadCount = 0
      }
      
      this.loadSessionMessages(session.targetUserId, session.goodsId)
    },
    
    async loadSessionMessages(targetUserId, goodsId) {
      try {
        const request = (await import('../utils/request')).default
        const userId = this.getCurrentUserId()
        const res = await request.get(`/chat/history/${userId}/${targetUserId}`)
        
        if (res.data.code === 200) {
          this.currentMessages = res.data.data
          
          if (goodsId) {
            const goodsRes = await request.get(`/goods/${goodsId}`)
            if (goodsRes.data.code === 200) {
              this.currentContextGoods = goodsRes.data.data
            }
          }
        }
      } catch (error) {
        console.error('加载消息历史失败:', error)
      }
    },
    
    sendMessage(content, goodsId = null) {
      if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
        console.error('WebSocket未连接')
        return
      }
      
      const msg = {
        senderId: this.getCurrentUserId(),
        receiverId: this.activeSessionId,
        goodsId: goodsId || this.currentContextGoods?.id,
        content,
        createTime: new Date().toISOString(),
        status: 0
      }
      
      this.socket.send(JSON.stringify(msg))
      this.currentMessages.push({ ...msg, isMine: true })
    },
    
    clearUnread(count) {
      this.unreadTotal = Math.max(0, this.unreadTotal - count)
    },
    
    closeSocket() {
      if (this.socket) {
        this.socket.close()
        this.socket = null
      }
    },
    
    reset() {
      this.unreadTotal = 0
      this.sessionList = []
      this.activeSessionId = null
      this.currentMessages = []
      this.currentContextGoods = null
    }
  },
})
