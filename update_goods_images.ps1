# 更新商品图片脚本
$base = "http://localhost:8080"
$uploadDir = "d:\_开发项目\校园二手交易平台-毕设\campus-trade\uploads"
$baseUrl = "http://localhost:8080"

# 获取所有商品
$goods = (Invoke-RestMethod "$base/goods/list?page=1&pageSize=50" -Method Get).data.records

# 为每个商品下载图片
foreach ($good in $goods) {
    $id = $good.id
    $title = $good.title
    
    echo "Processing product ID: $id"
    
    try {
        # 下载 picsum.photos 的图片
        $fileName = "goods_$id.jpg"
        $filePath = Join-Path $uploadDir $fileName
        $imageUrl = "https://picsum.photos/seed/product$id/400/400.jpg"
        
        Invoke-WebRequest -Uri $imageUrl -OutFile $filePath -UseBasicParsing
        echo "Image saved: $filePath"
        
        # 更新商品图片URL
        $newImageUrl = "$baseUrl/file/$fileName"
        $updateData = @{
            id = $id
            title = $good.title
            description = $good.description
            price = $good.price
            categoryName = $good.categoryName
            imageUrl = $newImageUrl
        } | ConvertTo-Json -Depth 10
        
        $updateResponse = Invoke-RestMethod "$base/goods/update" -Method Post -Body $updateData -ContentType "application/json"
        echo "Product $id image updated successfully"
    }
    catch {
        echo "Product $id failed: $($_.Exception.Message)"
    }
    
    Start-Sleep -Seconds 1
}

echo "All product images updated!"
