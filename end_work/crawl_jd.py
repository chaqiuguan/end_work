# -*- coding: utf-8 -*-
"""用requests+Cookie爬京东商品图片，每个产品3张图"""
import subprocess, json, os, time, re
import requests
from urllib.parse import quote

COOKIE_FILE = 'D:/NEUwork/web/p/jd_cookie.txt'
PRODUCTS_DIR = 'd:/NEUwork/web/end_work/frontend/public/products'
os.makedirs(PRODUCTS_DIR, exist_ok=True)

# 解析cookie
cookie_str = open(COOKIE_FILE, 'r', encoding='utf-8').read().strip()
session = requests.Session()
for item in cookie_str.split('; '):
    if '=' in item:
        k, v = item.split('=', 1)
        session.cookies.set(k, v, domain='.jd.com')

session.headers.update({
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36',
    'Referer': 'https://www.jd.com/',
})

def run_sql(sql):
    path = 'D:/NEUwork/web/p/_jd_tmp.sql'
    with open(path, 'w', encoding='utf-8') as f:
        f.write(sql)
    subprocess.run(['docker', 'cp', path, 'zhuanbaomao-mysql:/tmp/_jd_tmp.sql'], capture_output=True)
    subprocess.run(['docker', 'exec', 'zhuanbaomao-mysql', 'mysql', '-u', 'root', '-proot123456',
                    '--default-character-set=utf8mb4', 'zhuanbaomao', '-e', 'source /tmp/_jd_tmp.sql'], capture_output=True)

def get_products():
    r = subprocess.run(['docker', 'exec', 'zhuanbaomao-mysql', 'mysql', '-u', 'root', '-proot123456',
        '--default-character-set=utf8mb4', 'zhuanbaomao', '-N', '-e',
        "SELECT id, title FROM product WHERE deleted=0 AND images LIKE '%picsum%' ORDER BY id"], capture_output=True)
    lines = r.stdout.decode('utf-8', errors='replace').strip().split('\n')
    return [(int(l.split('\t')[0]), l.split('\t')[1]) for l in lines if '\t' in l]

def search_images(keyword, max_imgs=3):
    """搜索京东商品，提取图片URL"""
    url = f'https://search.jd.com/Search?keyword={quote(keyword)}&enc=utf-8&page=1'
    try:
        resp = session.get(url, timeout=10)
        # JD search page returns HTML with image URLs in data-img attribute
        html = resp.text
        # Pattern: "data-img":"1" src="//imgXX.360buyimg.com/..."
        imgs = re.findall(r'//img1[0-9]?\.360buyimg\.com/[^"\']+\.(?:jpg|jpeg|png)', html)
        # Deduplicate and prefix https
        seen = set()
        result = []
        for img in imgs:
            full = 'https:' + img
            if full not in seen:
                seen.add(full)
                result.append(full)
            if len(result) >= max_imgs:
                break
        return result
    except Exception as e:
        print(f'    搜索异常: {e}')
        return []

def download_img(url, filepath):
    try:
        resp = session.get(url, timeout=10, headers={'Referer': 'https://www.jd.com/'})
        if resp.status_code == 200 and len(resp.content) > 1000:
            with open(filepath, 'wb') as f:
                f.write(resp.content)
            return True
    except:
        pass
    return False

# 主流程
products = get_products()
print(f'待爬取: {len(products)} 件商品')

done = skipped = 0
for pid, title in products:
    try:
        # 搜索
        imgs = search_images(title[:20])
        if not imgs:
            skipped += 1
            print(f'  无图: {pid} {title[:30]}')
            time.sleep(0.5)
            continue

        # 下载
        paths = []
        for i, img_url in enumerate(imgs):
            fname = f'{pid}_{i}.jpg'
            fpath = os.path.join(PRODUCTS_DIR, fname)
            if download_img(img_url, fpath):
                paths.append(f'/products/{fname}')

        if not paths:
            skipped += 1
            continue

        # 更新数据库
        imgs_json = json.dumps(paths, ensure_ascii=False).replace("'", "''")
        run_sql(f"UPDATE product SET images = '{imgs_json}' WHERE id = {pid};")

        done += 1
        if done % 10 == 0:
            print(f'进度: {done}/{len(products)}')
        time.sleep(1.5)

    except Exception as e:
        skipped += 1
        print(f'  错误 {pid}: {e}')
        time.sleep(2)

print(f'\n完成: {done} 更新, {skipped} 跳过')
print(f'剩余: {119 - done} 件 (可手动补充)')
