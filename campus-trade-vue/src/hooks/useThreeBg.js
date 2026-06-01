import * as THREE from 'three'
import { onMounted, onBeforeUnmount } from 'vue'

export function useThreeBg(containerRef) {
  let scene, camera, renderer, mesh, animationId

  const initThreeJS = () => {
    if (!containerRef.value) return

    scene = new THREE.Scene()
    camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000)
    camera.position.z = 30

    renderer = new THREE.WebGLRenderer({
      alpha: true,
      antialias: true
    })

    renderer.setSize(window.innerWidth, window.innerHeight)
    containerRef.value.appendChild(renderer.domElement)

    const geometry = new THREE.IcosahedronGeometry(15, 1)

    const material = new THREE.MeshBasicMaterial({
      color: 0x409eff,
      wireframe: true,
      transparent: true,
      opacity: 0.25
    })

    mesh = new THREE.Mesh(geometry, material)
    scene.add(mesh)

    const animate = () => {
      animationId = requestAnimationFrame(animate)
      mesh.rotation.x += 0.002
      mesh.rotation.y += 0.003
      renderer.render(scene, camera)
    }
    animate()

    window.addEventListener('resize', handleResize)
  }

  const handleResize = () => {
    if (camera && renderer) {
      camera.aspect = window.innerWidth / window.innerHeight
      camera.updateProjectionMatrix()
      renderer.setSize(window.innerWidth, window.innerHeight)
    }
  }

  onMounted(() => {
    setTimeout(() => {
      initThreeJS()
    }, 100)
  })

  onBeforeUnmount(() => {
    if (animationId) {
      cancelAnimationFrame(animationId)
    }
    window.removeEventListener('resize', handleResize)
    if (renderer) {
      renderer.dispose()
    }
  })
}