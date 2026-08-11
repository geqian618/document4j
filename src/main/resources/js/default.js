function doDrag() {
    const container = document.querySelector('.container');
    const dragBar = document.querySelector('.drag-bar');
    const aside = document.querySelector('.aside');
    const content = document.querySelector('.content');
    const contentMinWidth = Number.parseInt(window.getComputedStyle(content).minWidth.replace('px', ''));
    const asideMinWidth = Number.parseInt(window.getComputedStyle(aside).minWidth.replace('px', ''));
    const dragBarWidth = dragBar.clientWidth;
    // 鼠标按下
    const onMouseDown = (e) => {
        // 鼠标开始的X轴坐标
        const startX = e.clientX;
        // 拖动块距离屏幕左侧的偏移量
        const offsetLeft = dragBar.offsetLeft;
        // 禁止文本选择（防止出现蓝色背景）
        document.body.style.userSelect = 'none';
        // 鼠标移动
        const onMouseMove = (e) => {
            // 拖动块的最大移动距离
            let maxWidth = container.clientWidth - contentMinWidth;
            // 鼠标箭头移动位置
            let pointerOffset = offsetLeft + (e.clientX - startX);
            // 边界值处理
            pointerOffset < asideMinWidth && (pointerOffset = asideMinWidth);
            pointerOffset > maxWidth && (pointerOffset = maxWidth);
            // 设置值（保证拖动块一直在拖动盒子的相对位置）
            aside.style.width = pointerOffset + 'px';
            dragBar.style.left = (pointerOffset === maxWidth) ? pointerOffset + 'px' : (pointerOffset - dragBarWidth / 2) + 'px';
            content.style.left = pointerOffset + 'px';
            content.style.width = (container.clientWidth - pointerOffset) + 'px';
            // 阻止默认行为，防止浏览器默认的文本选择等
            e.preventDefault();
        };
        // 鼠标松开
        const onMouseUp = () => {
            // 移除事件监听器
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
            // 恢复文本选择
            document.body.style.userSelect = 'auto';
        };
        // 绑定鼠标移动和松开事件
        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp, {once: true});
        // 防止文本选中和其他默认操作
        return false;
    };
    // 绑定鼠标按下事件
    dragBar.addEventListener('mousedown', onMouseDown);
}

function buildMenu() {
    const headings = document.querySelector(".content").querySelectorAll('h1, h2, h3, h4, h5, h6');
    const aside = document.querySelector(".aside");
    // 目录标题（固定，不随菜单滚动）
    const header = document.createElement("div");
    header.className = "aside-header";
    header.textContent = "文档目录";
    aside.appendChild(header);
    // 菜单滚动容器
    const menu = document.createElement("nav");
    menu.className = "aside-menu";
    aside.appendChild(menu);

    const levelOf = (heading) => Number.parseInt(heading.tagName.replace("H", ""));

    // 精致线条图标（feather 风格描边 SVG，颜色由 CSS currentColor 控制）
    const ICON_SVG = {
        book: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>',
        'book-open': '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>',
        'file-text': '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>'
    };
    // 按标题级别分配图标：一级=合上的书，二级=展开的书，三级及以上=文档页
    const iconNameOf = (level) => level === 1 ? "book" : (level === 2 ? "book-open" : "file-text");

    // 为每个标题分配锚点 id
    headings.forEach((heading, i) => {
        heading.id = "item" + i;
    });

    // 预计算每个标题是否有子标题：
    // 在其子树范围内（到下一个同级或更高级别标题之前），若存在更深的标题则视为有子标题
    const hasChildren = [];
    for (let i = 0; i < headings.length; i++) {
        const level = levelOf(headings[i]);
        let has = false;
        for (let j = i + 1; j < headings.length; j++) {
            const nextLevel = levelOf(headings[j]);
            if (nextLevel <= level) {
                break; // 已离开当前标题的子树范围
            }
            if (nextLevel > level) {
                has = true;
                break;
            }
        }
        hasChildren.push(has);
    }

    let activeItem = null;
    // 栈：保存每一层"可折叠分组"的子容器，用于把后续标题挂到正确的层级
    const stack = [];

    for (let i = 0; i < headings.length; i++) {
        const heading = headings[i];
        const level = levelOf(heading);

        // 弹出所有不低于当前级别的栈项，当前标题挂到最近一个更高级别的容器下
        while (stack.length && stack[stack.length - 1].level >= level) {
            stack.pop();
        }
        const parent = stack.length ? stack[stack.length - 1].container : menu;

        if (hasChildren[i]) {
            // 有子标题：生成可折叠分组节点（每一级都可以折叠）
            const group = document.createElement("div");
            group.className = "tree-group";

            const node = document.createElement("div");
            node.className = "tree-item tree-parent";
            const arrow = document.createElement("span");
            arrow.className = "tree-arrow";
            const icon = document.createElement("span");
            const iconName = iconNameOf(level);
            icon.className = "tree-icon " + iconName;
            icon.innerHTML = ICON_SVG[iconName];
            const text = document.createElement("span");
            text.className = "tree-text";
            text.textContent = heading.textContent;
            node.appendChild(arrow);
            node.appendChild(icon);
            node.appendChild(text);
            // 点击分组节点：展开 / 折叠
            node.addEventListener('click', () => {
                group.classList.toggle('closed');
            });

            const sub = document.createElement("div");
            sub.className = "tree-sub";

            group.appendChild(node);
            group.appendChild(sub);
            parent.appendChild(group);

            stack.push({container: sub, level: level});
        } else {
            // 无子标题：叶子节点，点击跳转锚点
            const leaf = document.createElement("a");
            leaf.className = "tree-item tree-leaf";
            leaf.href = '#item' + i;
            const icon = document.createElement("span");
            const iconName = iconNameOf(level);
            icon.className = "tree-icon " + iconName;
            icon.innerHTML = ICON_SVG[iconName];
            const text = document.createElement("span");
            text.className = "tree-text";
            text.textContent = heading.textContent;
            leaf.appendChild(icon);
            leaf.appendChild(text);
            leaf.addEventListener('click', () => {
                if (activeItem) {
                    activeItem.classList.remove('active');
                }
                leaf.classList.add('active');
                activeItem = leaf;
            });
            parent.appendChild(leaf);
        }
    }
}

function showCatalogue(required) {
    if (!required) {
        const page = document.querySelector(".page-layout");
        const aside = document.querySelector(".aside");
        const drag = document.querySelector(".drag-bar");
        const content = document.querySelector(".content");
        aside.remove()
        drag.remove()
        content.style.position = "static"
        content.style.width = "100%"
        content.style.top = ""
        content.style.left = ""
        content.style.overflowY = 'visible'
        page.style.padding = '0 15%'
        page.style.overflowX = 'hidden'
    }
}

doDrag();
buildMenu();