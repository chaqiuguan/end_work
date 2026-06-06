import re, os

BASE = 'd:/NEUwork/web/end_work/backend/src/main/java/com/zhuanbaomao/controller'

for fname, cache_name in [('CategoryController.java', 'categories'), ('AnnouncementController.java', 'announcements')]:
    path = os.path.join(BASE, fname)
    if not os.path.exists(path): continue
    c = open(path, encoding='utf-8').read()
    # Add imports if not present
    if 'import org.springframework.cache.annotation.Cacheable' not in c:
        c = c.replace('import lombok.RequiredArgsConstructor;',
            'import lombok.RequiredArgsConstructor;\nimport org.springframework.cache.annotation.CacheEvict;\nimport org.springframework.cache.annotation.Cacheable;')
    # Add Cacheable on list method
    old = '@GetMapping("/' + fname.replace('Controller.java','').lower() + '/list")'
    new = '@Cacheable(value = "' + cache_name + '", key = "\'public\'")\n    ' + old
    c = c.replace(old, new)
    # Add CacheEvict on write methods
    c = re.sub(r'(public Result<\?> (add|update|delete|save)\()', '@CacheEvict(value = "' + cache_name + '", allEntries = true)\n    \\1', c)
    open(path, 'w', encoding='utf-8').write(c)
    print(f'{fname}: cache added')

print('Done')
